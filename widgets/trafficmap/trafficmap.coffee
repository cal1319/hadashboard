class Dashing.Trafficmap extends Dashing.Widget

  ready: ->
    $(@node).removeClass('widget')
    if  $(@node).data('zoom')
      zoom =  $(@node).data('zoom')
      lat  = $(@node).data('lat')
      long = $(@node).data('long')
    else
      zoom = 12
    options =
      zoom: zoom
      center: {lat: 38.4684253, lng: -121.6969937 }
      disableDefaultUI: false
      draggable: true
      scrollwheel: false
      disableDoubleClickZoom: false

    @map = new google.maps.Map $(@node)[0], options
    @traffic = new google.maps.TrafficLayer
    @traffic.setMap(@map)
