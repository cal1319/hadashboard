class Dashing.Radar extends Dashing.Widget  
	ready: ->

	onData: (data) -> 
	onClick: (node, event) ->
  	Dashing.cycleDashboardsNow(boardnumber: ('main'), stagger: ("false"), fastTransition:('true'), transitiontype: @get('transitiontype'))
  
  
