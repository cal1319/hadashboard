class Dashing.Radar extends Dashing.ClickableWidget  
	ready: ->

	onData: (data) -> 
	onClick: (node, event) ->
  	Dashing.cycleDashboardsNow(boardnumber: ('main'), stagger: ("false"), fastTransition:('true'), transitiontype: @get('transitiontype'))
  
  
