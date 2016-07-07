addOnload(function() {
  document.getElementById('hamburger').addEventListener('click', function(e) {
    var nav = document.getElementById('sliding-nav');
    if (nav.className == 'sliding active') {
      nav.className = 'sliding';
    } else {
      nav.className = 'sliding active';
    }
  })
})
