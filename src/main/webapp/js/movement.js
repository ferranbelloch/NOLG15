console.log("-> movement.js cargado correctamente");  

const signInButton = document.getElementById('studentLogin'); 
const signUpButton = document.getElementById('teacherLogin'); 
const container = document.getElementById('container');

signInButton.addEventListener('click', () => {
	container.classList.remove("right-panel-active");
});

signUpButton.addEventListener('click', () => {
  container.classList.add("right-panel-active");
});