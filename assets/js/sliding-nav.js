addOnload(function() {
  var hamburger = document.getElementById('hamburger');
  var nav = document.getElementById('sliding-nav');
  hamburger.addEventListener('click', function(e) {
    if (nav.className == 'sliding') {
      nav.className = 'sliding active';
    } else {
      nav.className = 'sliding';
    }

    if (hamburger.className == 'hamburger') {
      hamburger.className = 'hamburger active';
    } else {
      hamburger.className = 'hamburger';
    }
  })
})
