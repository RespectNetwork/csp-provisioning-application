$(document).ready(function(){
		$('#registration-form').validate({
	    rules: {
	       name: {
	        required: false
	      },
		  
		 username: {
	        minlength: 6,
	        required: false
	      },
		  mobilePhone: {
			  phonenumber: true,
		  },
		  password: {
				required: true,
				atleasttwo: true,
				passtestletters: true,
				passtestnumbers: true,
				passtestchars: true,
				passtestlength: true,
			},
			confirmPassword: {
				required: true,
				equalTo: "#password"
			},
		  
	      email: {
	        required: false,
	        email: true
	      },
		  
	     
		   address: {
	      	minlength: 10,
	        required: false
	      },
		  
		  agree: "required"
		  
	    },
			highlight: function(element) {
				$(element).closest('.formGroup').removeClass('success').addClass('error');
			},
			success: function(element) {
				element
				.text('OK!').addClass('valid')
				.closest('.formGroup').removeClass('error').addClass('success');
			}
	  });

}); // end document.ready