class Dashing.Trafficmap extends Dashing.Widget

  ready: ->
    $(@node).removeClass('widget')
    if  $(@node).data('zoom')
      zoom =  $(@node).data('zoom')
      lat  = $(@node).data('lat')
      long = $(@node).data('long')
    else
      zoom = 13
    options =
      zoom: zoom
      center: {lat: lat, lng: long }
      disableDefaultUI: false
      draggable: true
      scrollwheel: false
      disableDoubleClickZoom: false

    @map = new google.maps.Map $(@node)[0], options
    @traffic = new google.maps.TrafficLayer
    @traffic.setMap(@map)
    google.maps.event.trigger(@map, "resize");
    @traffic.setMap(@map)
