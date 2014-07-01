$(document).ready(function(){
                $('#registration-form').validate({
            rules: {
                  mobilePhone: {
                          required : true,
                          phonenumber : true
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
                required: true,
                email: true
              },

                  agree:  {
                required: true
              },

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

          $('#change-password-form').validate({
              rules: {
                  newPassword: {
                      required: true,
                      atleasttwo: true,
                      passtestletters: true,
                      passtestnumbers: true,
                      passtestchars: true,
                      passtestlength: true,
                  },
                  confirmPassword: {
                      required: true,
                      equalTo: "#newPassword"
                  },
                  agree:  {
                      required: true
                  },
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


          $('#reset-password-form').validate({
              rules: {
                  newPassword: {
                      required: true,
                      atleasttwo: true,
                      passtestletters: true,
                      passtestnumbers: true,
                      passtestchars: true,
                      passtestlength: true,
                  },
                  confirmPassword: {
                      required: true,
                      equalTo: "#newPassword"
                  },
                  agree:  {
                      required: true
                  },
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
