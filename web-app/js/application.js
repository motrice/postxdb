/* == Motrice Copyright Notice ==
 * Motrice BPM. Copyright (C) 2011-2015 Motrice AB
 */
if (typeof jQuery !== 'undefined') {
	(function($) {
		$('#spinner').ajaxStart(function() {
			$(this).fadeIn();
		}).ajaxStop(function() {
			$(this).fadeOut();
		});
	})(jQuery);
}
