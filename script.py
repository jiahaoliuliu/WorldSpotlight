#!/usr/bin/env python
# coding=utf-8

import urllib2, json, requests
from parse_rest.connection import ParseBatcher
from parse_rest.datatypes import Object, GeoPoint
from parse_rest.connection import register

class Video(Object):
	pass

video_ids = []
landmarks = []
videos = []
keys={
	'API_KEY':'',
	'APPLICATION_ID':'',
	'REST_API_KEY':'',
	'MASTER_KEY':''
}

def parse(city, landmark, country):
	name,lat,lng = landmark['name'], landmark['geometry']['location']['lat'], landmark['geometry']['location']['lng']
	url_query = 'https://www.googleapis.com/youtube/v3/search?part=snippet&location=' + str(lat) +'%2C+' + str(lng) + '&locationRadius=50km&maxResults=50&order=date&q="GoPro"&safeSearch=moderate&type=video&videoDimension=2d&key='+ keys['API_KEY']
	response = urllib2.urlopen(url_query)
	obj2 = json.loads(response.read())
	
	global videos
	for obj in obj2['items']:
		if obj['id']['videoId'] not in video_ids:
			video_ids.append(obj['id']['videoId'])
			vidobj = Video(title=obj['snippet']['title'], 
				videoId=obj['id']['videoId'], 
				description=obj['snippet']['description'])
			vidobj.location = GeoPoint(latitude=lat, longitude=lng)
			vidobj.city=city
			vidobj.country=country
			#return vidobj
			videos.append(vidobj)
			break

	
def landmarks(city, country):
	city=city.replace(' ', '+')
	url = 'https://maps.googleapis.com/maps/api/geocode/json'
	params = {'address': city}
	#params = {'address': city + ','+ country}
	resp = requests.get(url=url, params=params, verify=False)
	url_data = json.loads(resp.text)['results'][0]

	try:
		temp = url_data['geometry']['location']
		lat, lng = str(temp['lat']), str(temp['lng'])
	except Exception, e:
		lat, lng = '', ''
	
	try:
		temp_list = url_data['address_components']
		for temp in temp_list:
			if 'country' in temp['types']:
				country=temp['long_name']
				break
	except Exception, e:
		country=''
	
	url = 'https://maps.googleapis.com/maps/api/place/nearbysearch/json?rankby=prominence&location='+lat+','+lng+'&key='+ keys['API_KEY']+'&radius=500000'
	response = urllib2.urlopen(url)
	obj2 = json.loads(response.read())['results']
	register(keys['APPLICATION_ID'], keys['REST_API_KEY'], master_key=keys['MASTER_KEY'])
	try:
		for landmark in obj2:
			parse(city.replace('+', ' '), landmark, country) 
	except Exception,e:
		print city, "Error:", e
	global videos
	if len(videos)>0:
		batcher = ParseBatcher()
		batcher.batch_save(videos)
		videos=[]

if __name__ == "__main__":
	with open("keys.txt") as myfile:
		for line in myfile:
			name, var = line.partition("=")[::2]
			keys[str(name.strip())] = str(var.strip())
	

	cities = []
	done = ['Goa', 'Kathmandu', 'Oslo', 'St Petersburg', 'Doha', 'Bucharest', 'Budapest', 'Stockholm', 'Al Ain', 'Abu Dhabi', 'Glasgow', 'Birmingham', 'Montreal', 'Chicago', 'Lisbon', 'Dallas', 'Bangkok', 'Los Angeles', 'Taipei', 'Milan', 'Seoul', 'Hong Kong', 'Kuala Lumpur', 'Florida', 'Washington', 'San Francisco', 'Osaka', 'Las Vegas', 'Damascus', 'Madina', 'Mecca', 'Santiago', 'Sao Paulo', 'Brasilia', 'Colombia', 'Interlaken', 'Candy', 'Bangalore', 'Wellington', 'Pune', 'Sharjah', 'Fujairah', 'Copenhagen', 'Amsterdam', 'London', 'Tripoli', 'Buenos Aires', 'Ecuador', 'Caracas', 'El Salvador', 'Nairobi', 'Ontario', 'Riyadh', 'Johannesburg', 'Cape Town', 'Colombo', 'Tibet', 'Bhutan', 'Novosibirsk', 'Saint Petersburg', 'Perth', 'Adelaide', 'Melbourne', 'Sydney', 'Tehran', 'Muscat', 'Brussels', 'Bali', 'Honolulu', 'Edinburgh', 'Wellington', 'Jakarta', 'Zurich', 'Dublin', 'Miami', 'Shanghai', 'Istanbul', 'Cairo', 'Prague', 'Vienna', 'Rio de Janeiro', 'Berlin', 'Tokyo', 'Mexico City', 'Munich', 'Boston', 'Baghdad', 'Warsaw', 'Johannesburg', 'Moscow', 'Mumbai', 'Delhi', 'Kolkata', 'Chennai', 'Lahore', 'Karachi', 'Dammam', 'Barcelona', 'Rome', 'Egypt', 'Cape Town', 'Krakow', 'Brazil', 'Florence', 'Peru', 'Paris', 'Canberra', 'Hamburg', 'Venice', 'Sydney', 'Rome', 'Maldives', 'Singapore']
	cities = list(set(cities)-set(done))
	for city in cities:
		landmarks(city, '')
