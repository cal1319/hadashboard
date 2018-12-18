require "net/http"
require "json"


# If these are left blank, the script will try to locate you using your IP address
latitude = "38.5378863"                   # Required
longitude = "-121.57037609999998"                  # Required
location = "West Sacramento,<br>California"   # Change me

units = "us"
symbol = "f"
key = "353b2c70b063c646e905d67cc2867088"                        # Required



    
SCHEDULER.every "15m", :first_in => 0 do |job|

    uri = URI("https://api.darksky.net/forecast/#{key}/#{latitude},#{longitude}?units=#{units}")
    req = Net::HTTP::Get.new(uri.path)

    # Make request
    res = Net::HTTP.start(
            uri.host, uri.port, 
            :use_ssl => uri.scheme == 'https', 
            :verify_mode => OpenSSL::SSL::VERIFY_NONE) do |https|
      https.request(req)
    end

    response = JSON.parse res.body
    
    currentResults = response["currently"]
    dailyResults  = response["daily"]["data"]
        
    forecasts = []
    
    #Today
    today = {}
    
    if currentResults
        
        currentTemp = symbol == "F" ? "#{(currentResults["temperature"]).round}°#{symbol}" : "#{currentResults["temperature"].round}°#{symbol}"
        currentlyIcon = currentResults["icon"]
        currentHigh = (dailyResults[0]["temperatureMax"]).round
        currentLow = (dailyResults[0]["temperatureMin"]).round
        currentSummary = response["hourly"]["summary"]
        todaysSummary = "High of #{currentHigh} with a low of #{currentLow}. #{currentSummary}"
            
        # Create object for this current day
        today = {
                temp: currentTemp,
                summary:  todaysSummary,
                code: currentlyIcon,
                element: 'currentWeatherIcon',
                location: location
            }

    end
    
    #Future Days

    if dailyResults

        # Create weather object for the next 5 days
        for day in (1..5) 
        
            day = dailyResults[day]
        
            # Format date as a qualified day i.e. Monday
            time = Time.at(day["time"]).strftime("%A")
            summary = day["summary"]
            
                
            # Create object for the day to send back to the widget
            this_day = {
                high: max.round,
                low:  min.round,
                date: time,
                code: day["icon"],
                text: day["text"], 
                
                element: 'weather-icon'
            }
            forecasts.push(this_day)
        end

        send_event "weeklyweather", { forecasts: forecasts, today: today }
    end


end
