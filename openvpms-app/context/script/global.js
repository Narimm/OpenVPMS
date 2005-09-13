

/*
 *    open a new window 
 *
 */
var newWindow = false;
 
function pop(fullAnchor, winTitle, winFeatures){   
	var realHref = '';
	if(fullAnchor.getAttribute) realHref = fullAnchor.getAttribute('href');
	if(realHref=='') realHref = fullAnchor.href;
    if( ! newWindow || newWindow.closed ) {
        newWindow = window.open(realHref,winTitle,winFeatures);
    }  else  {
        newWindow.location.href = realHref;
    } 
    if (newWindow) newWindow.focus();
    return (newWindow) ? false : true;
}

