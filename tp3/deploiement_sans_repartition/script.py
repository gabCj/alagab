import requests
import time
from threading import Thread

url = 'http://132.207.89.151:8080'

def sendRequest():
    r = requests.get(url)
    print(r.text)

threads = []

start = time.time()

for x in range(50):
  t = Thread(target=sendRequest)
  t.start()
  threads.append(t)

for t in threads:
  t.join()
  
end = time.time()
print(end-start)
