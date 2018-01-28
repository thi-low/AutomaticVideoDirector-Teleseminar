Project Overview
====
This is a re-upload of a university project from 2014 !!!!!!!

# Possible Description
Consider an event like a festival or concert with several thousand people. Many people may capture parts of the event with their smartphones. Due to the limited overall bandwidth, the upload of all videos is not possible during the event. This topic should consider the question which streams should be selected and uploaded to a central server in such a scenario.
The students’ task is to develop an algorithm that rates the quality of a video on the fly. The algorithm should be very fast so that decisions can be made nearly in real- time. Without a decision, each device should transfer a low bandwidth stream to the server, so that a director can decide on which video to transmit in higher quality. Because video analysis algorithms take too much time on a smartphone additional motion sensor data should be used to make this decision. High quality videos (based on motion information) should then be transferred to a server as soon as the (automatic) director on the server has chosen a suitable stream.



# Implemented Features

1. Android application which extracts motion sensor data captured by a smartphone to analyze the quality of a video.

2. Implements the data exchange between a server and several phones. The phones compute the video quality from sensor information and send it to the server. 

3. The server makes a decision about suitable video streams and sends notifications to the clients.

4. Notified clients upload their videos to the server.

Credits go to the team: 
	- https://github.com/egurnov
	- https://github.com/fwsGonzo
	- https://github.com/jonup
	- https://github.com/thi-low
