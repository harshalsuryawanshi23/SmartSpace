import requests
import time
from datetime import datetime, timedelta, timezone

BASE_URL = "http://localhost:8080/api/v1"

def test_slots():
    print("=== Starting Micro-Slots Test ===")
    ts = int(time.time())
    
    # 1. Register users
    print("Registering users...")
    requests.post(f"{BASE_URL}/auth/register", json={"fullName": "Owner", "email": f"owner_{ts}@test.com", "phone": f"9179000{ts%100000}", "password": "password123", "role": "HALL_OWNER"})
    requests.post(f"{BASE_URL}/auth/register", json={"fullName": "Renter", "email": f"renter_{ts}@test.com", "phone": f"9179001{ts%100000}", "password": "password123", "role": "RESIDENT"})
    requests.post(f"{BASE_URL}/auth/register", json={"fullName": "Admin", "email": f"admin_{ts}@test.com", "phone": f"9179002{ts%100000}", "password": "password123", "role": "ADMIN"})
    
    # 2. Login
    owner_token = requests.post(f"{BASE_URL}/auth/login", json={"identifier": f"owner_{ts}@test.com", "password": "password123"}).json()["accessToken"]
    renter_token = requests.post(f"{BASE_URL}/auth/login", json={"identifier": f"renter_{ts}@test.com", "password": "password123"}).json()["accessToken"]
    admin_token = requests.post(f"{BASE_URL}/auth/login", json={"identifier": f"admin_{ts}@test.com", "password": "password123"}).json()["accessToken"]
    
    owner_headers = {"Authorization": f"Bearer {owner_token}"}
    renter_headers = {"Authorization": f"Bearer {renter_token}"}
    admin_headers = {"Authorization": f"Bearer {admin_token}"}
    
    # 3. Create & Approve Society
    soc_res = requests.post(f"{BASE_URL}/owner/societies", json={
        "name": "Test Society",
        "addressLine": "123 Test St",
        "locality": "Kalyani Nagar",
        "city": "Pune",
        "pincode": "411014",
        "lat": 18.5,
        "lng": 73.9
    }, headers=owner_headers)
    soc_id = soc_res.json()["id"]
    requests.post(f"{BASE_URL}/admin/approvals/societies/{soc_id}", json={"approve": True}, headers=admin_headers)
    
    # 4. Create Hall & Approve
    hall_res = requests.post(f"{BASE_URL}/owner/halls", json={
        "societyId": soc_id,
        "name": "Slot Hall",
        "description": "A very nice hall",
        "addressLine": "123 Test St",
        "locality": "Kalyani Nagar",
        "city": "Pune",
        "lat": 18.5,
        "lng": 73.9,
        "capacitySeated": 100,
        "capacityStanding": 150,
        "areaSqft": 2000,
        "layoutType": "OPEN_HALL",
        "indoor": True,
        "basePricePerHour": 500.0,
        "minSlotMinutes": 60,
        "maxSlotMinutes": 480,
        "bufferAfterMinutes": 30,
        "advanceDaysPublic": 30,
        "advanceDaysMember": 60,
        "memberDiscountPercent": 0,
        "cancellationPolicy": "FLEXIBLE"
    }, headers=owner_headers)
    
    if hall_res.status_code != 200 and hall_res.status_code != 201:
        print("Hall Creation Failed:", hall_res.text)
    
    hall_id = hall_res.json()["id"]
    sub_res = requests.post(f"{BASE_URL}/owner/halls/{hall_id}/submit", headers=owner_headers)
    print("Submit Hall:", sub_res.status_code)
    app_res = requests.post(f"{BASE_URL}/admin/approvals/halls/{hall_id}", json={"approve": True}, headers=admin_headers)
    print("Approve Hall:", app_res.status_code)
    
    # 5. Bookings
    base_time = datetime.now(timezone.utc) + timedelta(days=2)
    base_time = base_time.replace(hour=0, minute=0, second=0, microsecond=0)
    
    def try_book(name, start_h, start_m, end_h, end_m):
        start = base_time + timedelta(hours=start_h, minutes=start_m)
        end = base_time + timedelta(hours=end_h, minutes=end_m)
        res = requests.post(f"{BASE_URL}/bookings", json={
            "idempotencyKey": f"k_{start_h}_{start_m}_{ts}",
            "hallId": hall_id,
            "eventTitle": f"Event {name}",
            "eventType": "BIRTHDAY",
            "guestCount": 50,
            "startAt": start.isoformat(),
            "endAt": end.isoformat()
        }, headers=renter_headers)
        try:
            print(f"{name} ({start_h:02d}:{start_m:02d} - {end_h:02d}:{end_m:02d}): Status {res.status_code} - {res.json()}")
        except:
            print(f"{name} ({start_h:02d}:{start_m:02d} - {end_h:02d}:{end_m:02d}): Status {res.status_code}")
        return res
        
    try_book("B1 (Valid)", 9, 0, 13, 0)
    try_book("B2 (Overlap)", 12, 0, 16, 0)
    try_book("B3 (Valid, respects 30m buffer)", 13, 30, 17, 30)
    try_book("B4 (Unaligned start)", 17, 45, 20, 0)
    try_book("B5 (Buffer violation, aligned)", 17, 30, 20, 0)
    
    print("=== Finished ===")

if __name__ == '__main__':
    test_slots()
