import requests
import json

base_url = 'http://localhost:8080/api/v1/dev'

def reset_demo():
    print("Calling /reset-demo...")
    r = requests.post(f"{base_url}/reset-demo")
    print(r.status_code)
    try:
        print(json.dumps(r.json(), indent=2))
    except:
        print(r.text)

def fast_forward(minutes):
    print(f"Calling /fast-forward-clock?minutes={minutes}...")
    r = requests.post(f"{base_url}/fast-forward-clock", params={"minutes": minutes})
    print(r.status_code)
    try:
        print(json.dumps(r.json(), indent=2))
    except:
        print(r.text)

if __name__ == '__main__':
    reset_demo()
    fast_forward(30)
