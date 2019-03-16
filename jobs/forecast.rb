require 'net/https'
require 'json'

# Forecast API Key from https://developer.forecast.io
forecast_api_key = ENV["Dark_Sky_Key"]

# Latitude, Longitude for location
forecast_location_lat = "38.5378863"
forecast_location_long = "-121.57037609999998"

# Unit Format
# "us" - U.S. Imperial
# "si" - International System of Units
# "uk" - SI w. windSpeed in mph
forecast_units = "us"

# Language
# check https://developer.forecast.io/docs/v2 for supported languages
lang = "en"

SCHEDULER.every '180m', :first_in => 0 do |job|
    uri = URI.parse("https://api.darksky.net")
    uri.path = "/forecast/#{forecast_api_key}/#{forecast_location_lat},#{forecast_location_long}"
    params = {
      :units   => forecast_units,
      :lang    => lang,
      :exclude => "alerts,flags"
    }
    uri.query = URI.encode_www_form(params)
    http = Net::HTTP.new(uri.host, uri.port)
    http.use_ssl = true
    http.verify_mode = OpenSSL::SSL::VERIFY_PEER
    response = http.request(Net::HTTP::Get.new(uri.request_uri))
    if not response.kind_of? Net::HTTPSuccess then raise "Forecast HTTP error" end
    forecast = JSON.parse(response.body)
    if forecast.has_key?("code") then raise "Forecast Error: #{forecast['code']}: #{forecast['error']}" end
    forecast_current_temp = forecast["currently"]["temperature"].round
    forecast_current_icon = forecast["currently"]["icon"]
    forecast_current_desc = forecast["currently"]["summary"]
    if forecast["minutely"]
       forecast_next_desc  = forecast["minutely"]["summary"]
       forecast_next_icon  = forecast["minutely"]["icon"]
    else
       puts "Did not get minutely forecast data again"
       forecast_next_desc  = "No data"
       forecast_next_icon  = ""
   end
       forecast_later_desc   = forecast["hourly"]["summary"]
       forecast_later_icon   = forecast["hourly"]["icon"]
       send_event('forecast', { current_temp: "#{forecast_current_temp}&deg;", current_icon: "#{forecast_current_icon}", current_desc: "#{forecast_current_desc}", next_icon: "#{forecast_next_icon}", next_desc: "#{forecast_next_desc}", later_icon: "#{forecast_later_icon}", later_desc: "#{forecast_later_desc}"})
   end
end
