require 'open-uri'

@cameraDelay = 1 # Needed for image sync. 
@fetchNewImageEvery = '180s'

@newFile1 = "assets/images/radarTahoe/new.jpg"
@oldFile1 = "assets/images/radarTahoe/old.jpg"

# Change "OHX" in the file << open... line to your radar station ID. Check README for link.
def fetch_image(old_file,new_file)
	`rm #{old_file}` 
	`mv #{new_file} #{old_file}`	
	open('assets/images/radar/new.jpg', 'wb') do |file|
		file << open('https://cdn.tegna-media.com/kxtv/weather/satradtahoe16x9.jpg').read
	end
	new_file
end

def make_web_friendly(file)
  "/" + File.basename(File.dirname(file)) + "/" + File.basename(file)
end

SCHEDULER.every @fetchNewImageEvery, first_in: 0 do
	new_file1 = fetch_image(@oldFile1,@newFile1)

	if not File.exists?(@newFile1)
		warn "Failed to Get Radar Image"
	end
 
	send_event('radar', image: make_web_friendly(@oldFile1))
	sleep(@cameraDelay)
	send_event('radar', image: make_web_friendly(new_file1))
end
