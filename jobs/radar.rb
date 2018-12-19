require 'open-uri'

@cameraDelay = 1 # Needed for image sync. 

@newFile1 = "assets/images/radar/new.jpg"
@oldFile1 = "assets/images/radar/old.jpg"

SCHEDULER.every "60s", first_in: 0 do |job|
	

# Change "OHX" in the file << open... line to your radar station ID. Check README for link.
def fetch_image(old_file,new_file)
	`rm #{old_file}` 
	`mv #{new_file} #{old_file}`	
	open('assets/images/radar/new.jpg', 'wb') do |file|
		file << open('https://cdns.abclocal.go.com/three/kgo/weather/maps/ncal1_1280.jpg').read
	end
	new_file
end

def make_web_friendly(file)
  "/" + File.basename(File.dirname(file)) + "/" + File.basename(file)
end


 
	send_event('radar', image: make_web_friendly(@oldFile1))
	sleep(@cameraDelay)
	send_event('radar', image: make_web_friendly(new_file1))
end
