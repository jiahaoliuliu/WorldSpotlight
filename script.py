#!/usr/bin/env python
# coding=utf-8

import urllib, urllib2, json, requests, httplib
from bs4 import BeautifulSoup
from parse_rest.connection import ParseBatcher
from parse_rest.datatypes import Object, GeoPoint
from parse_rest.connection import register

class Video(Object):
	pass

video_ids = []
landmarks = []

keys={
	'API_KEY':'',
	'APPLICATION_ID':'',
	'REST_API_KEY':'',
	'MASTER_KEY':''
}


	
#def parse(lat, lng):
def parse(landmark):
	name,lat,lng = landmark['name'], landmark['geometry']['location']['lat'], landmark['geometry']['location']['lng']
	#name, lat, lng = landmark['name1'], landmark['lat'], landmark['lon']
	url_query = 'https://www.googleapis.com/youtube/v3/search?part=snippet&location=' + str(lat) +'%2C+' + str(lng) + '&locationRadius=50km&maxResults=50&order=date&q="GoPro"&safeSearch=moderate&type=video&videoDimension=2d&key='+ keys['API_KEY']
	#url_query = 'https://www.googleapis.com/youtube/v3/search?part=snippet&locationRadius=50km&maxResults=50&order=date&q="GoPro" +' + str(landmark)+ '&safeSearch=moderate&type=video&videoDimension=2d&key='+ keys['API_KEY']
	response = urllib2.urlopen(url_query)
	obj2 = json.loads(response.read())
	register(keys['APPLICATION_ID'], keys['REST_API_KEY'], master_key=keys['MASTER_KEY'])

	for obj in obj2['items']:
		if obj['id']['videoId'] not in video_ids:
			video_ids.append(obj['id']['videoId'])
			vidobj = Video(title=obj['snippet']['title'], 
				videoId=obj['id']['videoId'], 
				description=obj['snippet']['description'])
			vidobj.location = GeoPoint(latitude=lat, longitude=lng)
			vidobj.save()
			break

	
def landmarks(city, country):
	url = 'https://maps.googleapis.com/maps/api/geocode/json'
	params = {'address': city}
	#params = {'address': city + ','+ country}

	resp = requests.get(url=url, params=params, verify=False)
	url_data = json.loads(resp.text)

	try:
		temp = url_data['results'][0]['geometry']['location']
		lat, lng = str(temp['lat']), str(temp['lng'])
	except Exception, e:
		lat, lng = '', ''
	
	url = 'https://maps.googleapis.com/maps/api/place/nearbysearch/json?location='+lat+','+lng+'&key='+ keys['API_KEY']+'&radius=500000'
	response = urllib2.urlopen(url)
	obj2 = json.loads(response.read())['results']
	try:
		for landmark in obj2:
			parse(landmark)
	except Exception,e:
		print city
	
def country_to_code(country):
	with open('country_code.json') as data_file:
		data = json.load(data_file)
	for cnt in data:
		if cnt['name']==country:
			return cnt['code']
			break
	#print data


if __name__ == "__main__":
	with open("keys.txt") as myfile:
		for line in myfile:
			name, var = line.partition("=")[::2]
			keys[str(name.strip())] = str(var.strip())
	
	'''
	country = 'China'
	#cnt = 'US'
	cnt = str(country_to_code(country))
	print cnt
	for city in data[country]:
		landmarks(city, cnt)
	
	'''
	print keys
	cities = ['Bangalore',]
	done = ['Wellington', 'Pune', 'Sharjah', 'Fujairah', 'Copenhagen', 'Amsterdam', 'London', 'Tripoli', 'Santiago', 'Buenos+Aires', 'Ecuador', 'Caracas', 'El Salvador', 'Nairobi', 'Ontario', 'Riyadh', 'Johannesburg', 'Cape Town', 'Colombo', 'Tibet', 'Bhutan', 'Novosibirsk', 'Saint+Petersburg', 'Perth', 'Adelaide', 'Melbourne', 'Sydney', 'Tehran', 'Muscat', 'Brussels', 'Bali', 'Honolulu', 'Edinburgh', 'Wellington', 'Jakarta', 'Zurich', 'Dublin', 'Miami', 'Shanghai', 'Istanbul', 'Cairo', 'Prague', 'Vienna', 'Rio de Janeiro', 'Berlin', 'Tokyo', 'Mexico+City', 'Munich', 'Florence', 'Boston', 'Baghdad', 'Warsaw', 'Johannesburg''Moscow', 'Mumbai', 'Delhi', 'Kolkata', 'Chennai', 'Lahore', 'Karachi', 'Dammam', 'Barcelona', 'Rome', 'Egypt', 'Cape Town', 'Krakow', 'Brazil', 'Florence', 'Peru', 'Paris', 'Canberra', 'Hamburg', 'Venice', 'Sydney', 'Rome', 'Maldives', 'Singapore']
	cities = list(set(cities)-set(done))
	for city in cities:
		landmarks(city, '')