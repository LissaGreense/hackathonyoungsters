import urllib.request, json, datetime
from collections import defaultdict
def nested_dict(n, type):
    if n == 1:
        return defaultdict(type)
    else:
        return defaultdict(lambda: nested_dict(n-1, type))

array = nested_dict(2, list)
now = datetime.datetime.now()
with urllib.request.urlopen("http://91.244.248.19/dataset/c24aa637-3619-4dc2-a171-a23eec8f2172/resource/cd4c08b5-460e-40db-b920-ab9fc93c1a92/download/stops.json") as url:
	data = json.loads(url.read().decode())
	for stop in data[str(now.date())]['stops']:
		stopID = stop['stopId']
		tempLat = int((stop['stopLat'] * 100000 )//10)
		tempLon = int((stop['stopLon'] * 100000)//10)
		for x in range (tempLat-10,tempLat + 10):
			for y in range (tempLon - 10,tempLon +10):
				array[x][y].append(stopID)
with open('data.json', 'w') as outfile:
    json.dump(array, outfile)