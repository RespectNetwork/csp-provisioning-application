function getDivElementsById(id)
{
	var rtn  = [];
	var divs = document.getElementsByTagName("div");
	for (var i=0; i < divs.length; i++)
	{
		if( divs[i].id === undefined )
		{
			continue;
		}
		if( divs[i].id == id )
		{
			rtn.push(divs[i]);
		}
	}
	return rtn;
}

function showInviteList(all)
{
        if( inviteListSize <= 4 )
        {
                return;
        }
        var i;
        var divs = getDivElementsById("inviteList")
	for( i = 4; i < divs.length; i++ )
	{
		divs[i].style.display = all ? 'block' : 'none';
	}
	document.getElementById("showAll" ).style.display = all ? 'none'  : 'block';
	document.getElementById("showLess").style.display = all ? 'block' : 'none' ;
}
