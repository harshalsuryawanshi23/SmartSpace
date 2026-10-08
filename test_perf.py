import requests
import time
import numpy as np
from datetime import datetime, timedelta, timezone

BASE_URL = "http://localhost:8080/api/v1"

def test_perf():
    print("=== Starting Performance Smoke Test ===")
    ts = int(time.time())
    
    # 1. Register users for booking
    requests.post(f"{BASE_URL}/auth/register", json={"fullName": "Owner", "email": f"owner_{ts}@test.com", "phone": f"9179200{ts%100000}", "password": "password123", "role": "HALL_OWNER"})
    requests.post(f"{BASE_URL}/auth/register", json={"fullName": "Renter", "email": f"renter_{ts}@test.com", "phone": f"9179201{ts%100000}", "password": "password123", "role": "RESIDENT"})
    requests.post(f"{BASE_URL}/auth/register", json={"fullName": "Watchman", "email": f"watchman_{ts}@test.com", "phone": f"9179202{ts%100000}", "password": "password123", "role": "WATCHMAN"})
    requests.post(f"{BASE_URL}/auth/register", json={"fullName": "Admin", "email": f"admin_{ts}@test.com", "phone": f"9179203{ts%100000}", "password": "password123", "role": "ADMIN"})
    
    owner_token = requests.post(f"{BASE_URL}/auth/login", json={"identifier": f"owner_{ts}@test.com", "password": "password123"}).json()["accessToken"]
    renter_token = requests.post(f"{BASE_URL}/auth/login", json={"identifier": f"renter_{ts}@test.com", "password": "password123"}).json()["accessToken"]
    watchman_token = requests.post(f"{BASE_URL}/auth/login", json={"identifier": f"watchman_{ts}@test.com", "password": "password123"}).json()["accessToken"]
    admin_token = requests.post(f"{BASE_URL}/auth/login", json={"identifier": f"admin_{ts}@test.com", "password": "password123"}).json()["accessToken"]
    
    owner_headers = {"Authorization": f"Bearer {owner_token}"}
    renter_headers = {"Authorization": f"Bearer {renter_token}"}
    watchman_headers = {"Authorization": f"Bearer {watchman_token}"}
    admin_headers = {"Authorization": f"Bearer {admin_token}"}
    
    # Create Society & Hall
    soc_res = requests.post(f"{BASE_URL}/owner/societies", json={"name": "Perf Society", "addressLine": "123 Test St", "locality": "Kalyani Nagar", "city": "Pune", "pincode": "411014", "lat": 18.5, "lng": 73.9}, headers=owner_headers)
    soc_id = soc_res.json()["id"]
    requests.post(f"{BASE_URL}/admin/approvals/societies/{soc_id}", json={"approve": True}, headers=admin_headers)
    
    hall_res = requests.post(f"{BASE_URL}/owner/halls", json={
        "societyId": soc_id, "name": "Perf Hall", "description": "Perf hall", "addressLine": "123 St", "locality": "Kalyani", "city": "Pune", "lat": 18.5, "lng": 73.9,
        "capacitySeated": 100, "capacityStanding": 150, "areaSqft": 2000, "layoutType": "OPEN_HALL", "indoor": True,
        "basePricePerHour": 500.0, "minSlotMinutes": 60, "maxSlotMinutes": 480, "bufferAfterMinutes": 30,
        "advanceDaysPublic": 90, "advanceDaysMember": 90, "memberDiscountPercent": 0, "cancellationPolicy": "FLEXIBLE"
    }, headers=owner_headers)
    hall_id = hall_res.json()["id"]
    requests.post(f"{BASE_URL}/owner/halls/{hall_id}/submit", headers=owner_headers)
    requests.post(f"{BASE_URL}/admin/approvals/halls/{hall_id}", json={"approve": True}, headers=admin_headers)
    
    # Assign watchman to society
    requests.post(f"{BASE_URL}/owner/societies/{soc_id}/watchmen", json={"watchmanEmail": f"watchman_{ts}@test.com"}, headers=owner_headers)
    
    start = datetime.now(timezone.utc) + timedelta(days=2)
    start = start.replace(hour=10, minute=0, second=0, microsecond=0)
    
    # Create 50 bookings
    print("Creating bookings for scan testing...")
    qr_codes = []
    for i in range(50):
        cur_start = start + timedelta(days=i)
        cur_end = cur_start + timedelta(hours=2)
        book_res = requests.post(f"{BASE_URL}/bookings", json={
            "idempotencyKey": f"k_{i}_{ts}",
            "hallId": hall_id,
            "eventTitle": f"Perf Event {i}",
            "eventType": "BIRTHDAY",
            "guestCount": 50,
            "startAt": cur_start.isoformat(),
            "endAt": cur_end.isoformat()
        }, headers=renter_headers)
        if book_res.status_code == 201:
            pub_id = book_res.json()["publicId"]
            
            # mock pay
            requests.post(f"{BASE_URL}/bookings/{pub_id}/mock-pay", headers=renter_headers)
            
            qr_res = requests.get(f"{BASE_URL}/entry/bookings/{pub_id}/qr", headers=renter_headers)
            if qr_res.status_code == 200:
                qr_codes.append(qr_res.json()["token"])
            else:
                print("QR fetch failed:", qr_res.status_code, qr_res.text)
        else:
            print("Booking failed:", book_res.status_code, book_res.text)
    
    # Test Scan Latency
    print(f"Testing /entry/scan with {len(qr_codes)} QRs...")
    scan_latencies = []
    for qr in qr_codes:
        start_time = time.time()
        res = requests.post(f"{BASE_URL}/entry/scan", json={"token": qr, "deviceId": "perf-device"}, headers=watchman_headers)
        end_time = time.time()
        scan_latencies.append((end_time - start_time) * 1000)
    
    if scan_latencies:
        p95_scan = np.percentile(scan_latencies, 95)
        print(f"Scan p95: {p95_scan:.2f} ms")
    else:
        print("No QR codes to scan.")
        
    print("=== Finished ===")

if __name__ == '__main__':
    test_perf()
