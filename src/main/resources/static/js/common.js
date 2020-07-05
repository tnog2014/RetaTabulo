

function processValidation(items){
	_.each(items, function(item){
		$('#' + item).on('change', function(){
			$(this).removeClass("fieldError");
			$('#err_' + item).remove();
		});
	})
}

function getBeforeQuestionMark(input){
	console.log(input);
	var ret = input;
	var index = ret.indexOf("?");
	if(index > 0){
		ret = ret.substring(0, index);
	}
	return ret;
}

function setUpLocaleChangeWithConfirmation(doConfirm){
	var $locales = $("#locales");
	$locales.data("prev", $locales.val());
	$("#locales").change(function () {
		var selectedOption = $('#locales').val();
		var result = true;
		if(doConfirm){
			result = confirm("言語を変更すると入力値がクリアされます。よろしいですか？");
		}
		if(result){
			url = window.location.pathname;
			url = getBeforeQuestionMark(url);
			window.location.replace(url + '?lang=' + selectedOption);
		} else {
			prev = $(this).data("prev");
			$(this).val(prev);
		}
	});
}

function setLocationHref(path){
	var locpath = contextPath + path;
	location.href=locpath;
}

// user
function openUser(id, from){
	var url = "user/" + id;
	if(from != null){
		url = url + "?from=" + from;
	}
	setLocationHref(url);
}

function openUserList(){
	setLocationHref("user");
}

function openUserCreate(){
	setLocationHref("user/create");
}

function openUserEdit(id, from){
	var url = "user/edit/" + id;
	if(from != null){
		url = url + "?from=" + from;
	}
	setLocationHref(url);
}

function deleteUser(id){
	setLocationHref("user/delete/" + id);
}
// team

function openTeam(id){
	setLocationHref("team/" + id);
}

function openTeamList(){
	setLocationHref("team");
}

function openTeamCreate(){
	setLocationHref("team/create");
}

function openTeamEdit(id){
	setLocationHref("team/edit/" + id);
}

function deleteTeam(id){
	setLocationHref("team/delete/" + id);
}

// board

function openBoard(id){
	var param = "";
	if(selectedTeam != null){
		param = "?st=" + selectedTeam;
	}
	setLocationHref("board/open/" + id + param);
}

function openBoardList(){
	setLocationHref("board");
}

function openBoardCreate(){
	setLocationHref("board/create");
}

function openBoardEdit(id){
	setLocationHref("board/edit/" + id);
}

function openBoardInfo(id){
	setLocationHref("board/" + id);
}

function deleteBoard(id){
	setLocationHref("board/delete/" + id);
}

function downloadHTML(id){
	setLocationHref("download/html/" + id);
}

function downloadCSV(id){
	setLocationHref("download/csv/" + id);
}
