require 'net/http'
require 'open-uri'

@url = 'https://cdns.abclocal.go.com/three/kgo/weather/maps/ncal1_1280.jpg'

SCHEDULER.every '4s', :first_in => 0 do |job|

    `find '/assets/images/radar/' -type f -mmin +1 -print0 | xargs -0 rm -f`

    @currentTime = Time.now.strftime("%Y-%m-%d_%H-%M-%S")
    @newFile1 = "assets/images/radar/snapshot-" + @currentTime + "_new.jpeg"

    open(@url, :http_basic_authentication => ['root', 'CamPW']) do |f|
      open(@newFile1,'wb') do |file|
        file.puts f.read
      end
    end

    send_event('radar', image: ("/" + File.basename(File.dirname(@newFile1)) + "/" + File.basename(@newFile1)))

end
