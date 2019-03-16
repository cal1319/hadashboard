class Dashing.Forecast extends Dashing.ClickableWidget

  # Overrides Dashing.Widget method in dashing.coffee
  @accessor 'updatedAtMessage', ->
    if updatedAt = @get('updatedAt')
      timestamp = new Date(updatedAt * 1000)
      hours = timestamp.getHours()
      minutes = ("0" + timestamp.getMinutes()).slice(-2)
      "Updated at #{hours}:#{minutes}"

  constructor: ->
    super
    @forecast_icons = new Skycons({"monochrome": false})
    @forecast_icons.play()

  ready: ->
    # This is fired when the widget is done being rendered
    @setIcons()

  onData: (data) ->
    # Handle incoming data
    # We want to make sure the first time they're set is after ready()
    # has been called, or the Skycons code will complain.
    if (data.current_icon && data.next_icon && data.later_icon)
      @setIcons()

  setIcons: ->
    @setIcon('current_icon')
    @setIcon('next_icon')
    @setIcon('later_icon')

  setIcon: (name) ->
    skycon = @toSkycon(name)
    @forecast_icons.set(name, eval(skycon)) if skycon

  toSkycon: (data) ->
    if @get(data)
      'Skycons.' + @get(data).replace(/-/g, "_").toUpperCase()
      
  onClick: (node, event) ->
  	Dashing.cycleDashboardsNow(boardnumber: ('weather'), stagger: ("false"), fastTransition:('true'), transitiontype: @get('transitiontype'))
