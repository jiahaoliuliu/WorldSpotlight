from parse_rest.datatypes import Object
from parse_rest.connection import register
from itertools import chain
'''Remove Duplicate Videos'''
keys={
	'API_KEY':'',
	'APPLICATION_ID':'',
	'REST_API_KEY':'',
	'MASTER_KEY':''
}

MAX_NO_OF_RECORD = 1000

class Video(Object):
	pass

video_ids = []

def destroy_duplicates(max_count):
	register(keys['APPLICATION_ID'], keys['REST_API_KEY'], 
		master_key=keys['MASTER_KEY'])
	
	count=MAX_NO_OF_RECORD
	all_videos = Video.Query.all().limit(MAX_NO_OF_RECORD)

	while count<max_count:
		all_videos=list(chain(all_videos, 
			Video.Query.all().skip(count).limit(MAX_NO_OF_RECORD)))
		count = count+MAX_NO_OF_RECORD
	
	#print len(all_videos)
	for video in all_videos:
		if video.videoId not in video_ids:
			video_ids.append(video.videoId)
		else:
			#print video.objectId
			video.delete()
	

if __name__ == '__main__':
	with open("keys.txt") as myfile:
		for line in myfile:
			name, var = line.partition("=")[::2]
			keys[str(name.strip())] = str(var.strip())
	destroy_duplicates(1600)