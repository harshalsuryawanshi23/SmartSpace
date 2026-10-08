import requests
import time
from datetime import datetime, timedelta, timezone

BASE_URL = "http://localhost:8080/api/v1"

def test_flow():
    print("=== Starting Complete E2E Flow ===")
    
    # 1. Register users
    email = f"resident_{int(time.time())}@example.com"
    watchman_email = f"watchman_{int(time.time())}@example.com"
    owner_email = f"owner_{int(time.time())}@example.com"
    password = "Password123!"
    
    print(f"Registering {owner_email} as HALL_OWNER...")
    res = requests.post(f"{BASE_URL}/auth/register", json={
        "fullName": "Test Owner",
        "email": owner_email,
        "phone": f"7{int(time.time())}",
        "password": password,
        "role": "HALL_OWNER"
    })
    print("Register OWNER:", res.status_code)

    print(f"Registering {email} as RESIDENT...")
    res = requests.post(f"{BASE_URL}/auth/register", json={
        "fullName": "Test Resident",
        "email": email,
        "phone": f"9{int(time.time())}",
        "password": password,
        "role": "RESIDENT"
    })
    print("Register RESIDENT:", res.status_code)
    
    print(f"Registering {watchman_email} as WATCHMAN...")
    res = requests.post(f"{BASE_URL}/auth/register", json={
        "fullName": "Test Watchman",
        "email": watchman_email,
        "phone": f"8{int(time.time())}",
        "password": password,
        "role": "WATCHMAN"
    })
    print("Register WATCHMAN:", res.status_code)
    
    # 2. Login
    print("Logging in as OWNER...")
    res = requests.post(f"{BASE_URL}/auth/login", json={
        "identifier": owner_email,
        "password": password
    })
    print("Login OWNER:", res.status_code)
    owner_token = res.json()["accessToken"]
    owner_headers = {"Authorization": f"Bearer {owner_token}"}

    print("Logging in as RESIDENT...")
    res = requests.post(f"{BASE_URL}/auth/login", json={
        "identifier": email,
        "password": password
    })
    print("Login RESIDENT:", res.status_code)
    renter_token = res.json()["accessToken"]
    renter_headers = {"Authorization": f"Bearer {renter_token}"}
    
    print("Logging in as WATCHMAN...")
    res = requests.post(f"{BASE_URL}/auth/login", json={
        "identifier": watchman_email,
        "password": password
    })
    print("Login WATCHMAN:", res.status_code)
    watchman_token = res.json()["accessToken"]
    watchman_headers = {"Authorization": f"Bearer {watchman_token}"}
    
    admin_email = f"admin_{int(time.time())}@example.com"
    print(f"Registering {admin_email} as ADMIN...")
    res = requests.post(f"{BASE_URL}/auth/register", json={
        "fullName": "Test Admin",
        "email": admin_email,
        "phone": f"6{int(time.time())}",
        "password": password,
        "role": "ADMIN"
    })
    print("Register ADMIN:", res.status_code)

    print("Logging in as ADMIN...")
    res = requests.post(f"{BASE_URL}/auth/login", json={
        "identifier": admin_email,
        "password": password
    })
    print("Login ADMIN:", res.status_code)
    admin_token = res.json()["accessToken"]
    admin_headers = {"Authorization": f"Bearer {admin_token}"}
    
    # 3. Create Society
    print("Creating Society...")
    res = requests.post(f"{BASE_URL}/owner/societies", json={
        "name": "Test Society",
        "addressLine": "123 Test St",
        "locality": "Kalyani Nagar",
        "city": "Pune",
        "pincode": "411014",
        "lat": 18.5,
        "lng": 73.9
    }, headers=owner_headers)
    print("Create Society:", res.status_code)
    society_id = res.json()["id"]

    print("Approving Society...")
    res = requests.post(f"{BASE_URL}/admin/approvals/societies/{society_id}", json={"approve": True, "reason": "Looks good"}, headers=admin_headers)
    print("Approve Society:", res.status_code)

    # 4. Create Hall
    print("Creating Hall...")
    res = requests.post(f"{BASE_URL}/owner/halls", json={
        "societyId": society_id,
        "name": "Test Hall",
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
        "basePricePerHour": 500.0,
        "minSlotMinutes": 60,
        "maxSlotMinutes": 360,
        "bufferAfterMinutes": 30,
        "cancellationPolicy": "FLEXIBLE",
        "overstayFeePer15Min": 100.0
    }, headers=owner_headers)
    print("Create Hall:", res.status_code)
    hall_id = res.json()["id"]

    print("Submitting Hall...")
    res = requests.post(f"{BASE_URL}/owner/halls/{hall_id}/submit", headers=owner_headers)
    print("Submit Hall:", res.status_code)

    print("Approving Hall...")
    res = requests.post(f"{BASE_URL}/admin/approvals/halls/{hall_id}", json={"approve": True, "reason": "Looks good"}, headers=admin_headers)
    print("Approve Hall:", res.status_code)

    # 5. Create Booking
    print("Creating booking...")
    start = datetime.now(timezone.utc) + timedelta(days=2)
    minutes = start.minute
    if minutes < 30:
        start = start.replace(minute=30, second=0, microsecond=0)
    else:
        start = start.replace(minute=0, second=0, microsecond=0) + timedelta(hours=1)
        
    end = start + timedelta(hours=4)
    
    booking_req = {
        "idempotencyKey": f"key_{int(time.time())}",
        "hallId": hall_id,
        "eventType": "BIRTHDAY",
        "eventTitle": "Test Birthday Party",
        "themeTags": ["Music", "Food"],
        "guestCount": 50,
        "startAt": start.isoformat(),
        "endAt": end.isoformat()
    }
    res = requests.post(f"{BASE_URL}/bookings", json=booking_req, headers=renter_headers)
    print("Create Booking:", res.status_code, res.text)
    if res.status_code != 201:
        print("Failed to create booking. Stopping.")
    
    print("Fetching My Bookings...")
    res = requests.get(f"{BASE_URL}/bookings", headers=renter_headers)
    print("My Bookings:", res.status_code)
    if res.status_code == 200:
        bookings = res.json()
        if bookings:
            public_id = bookings[0]["publicId"]
            print(f"Cancelling Booking {public_id}...")
            # Fetch specific booking
            res = requests.get(f"{BASE_URL}/bookings/{public_id}", headers=renter_headers)
            print("Get Booking:", res.status_code)
    
    print("Testing WaitlistController...")
    res = requests.post(f"{BASE_URL}/halls/{hall_id}/waitlist", json={
        "startAt": start.isoformat(),
        "durationMinutes": 120,
        "guestCount": 50
    }, headers=renter_headers)
    print("Waitlist:", res.status_code, res.text)
    
    print("Testing EntryController (GET /api/v1/entry/today)...")
    res = requests.get(f"{BASE_URL}/entry/today", headers=watchman_headers)
    print("Entry Today:", res.status_code, res.text)
    
    print("=== End E2E Flow ===")

if __name__ == "__main__":
    test_flow()
