function showCommits(branchName) {
    document.getElementById(branchName).style.visibility = 'visible';
    document.getElementById("h" + branchName).innerHTML = "<H3>Branch: " + branchName + "</H3>";
}

function hideCommits(branchName) {
    document.getElementById(branchName).style.visibility = 'hidden';
}

function showCommitterInfo(committerEmail) {
    document.getElementById(committerEmail).style.visibility = 'visible';
    document.getElementById("h" + committerEmail).innerHTML = "<H3>Committer: " + committerEmail + "</H3>";
}

function hideCommitterInfo(committerEmail) {
    document.getElementById(committerEmail).style.visibility = 'hidden';
}