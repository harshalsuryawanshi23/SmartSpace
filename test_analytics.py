import requests
from datetime import datetime, timedelta, UTC
import jwt
import sys

secret = 'V2h5RG9uJ3RZb3VKdXN0RGVjb2RlVGhpc0Jhc2U2NFN0cmluZ0FuZFRyeVRvRmluZFRoZVNlY3JldEtleU1heWJlSXQ='
payload = {
  'sub': 'owner@example.com',
  'userId': 1,
  'roles': ['OWNER'],
  'iat': datetime.now(UTC),
  'exp': datetime.now(UTC) + timedelta(minutes=15)
}
token = jwt.encode(payload, secret, algorithm='HS512')
headers = {'Authorization': f'Bearer {token}'}

start = (datetime.now(UTC) - timedelta(days=30)).isoformat()
end = datetime.now(UTC).isoformat()
url = f'http://localhost:8080/api/v1/owner/analytics/summary?start={start}&end={end}'

try:
    response = requests.get(url, headers=headers)
    print(f"Status: {response.status_code}")
    print(f"Response: {response.text}")
    if response.status_code != 200:
        sys.exit(1)
except Exception as e:
    print(e)
    sys.exit(1)
