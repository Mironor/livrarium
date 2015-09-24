angular.module('lvr')
    .constant('i18nEn', angular.extend({},
        {
            "login.login": "Login",
            "login.emailPlaceholder": "Email",
            "login.passwordPlaceholder": "Password",
            "login.rePasswordPlaceholder": "Confirm password",
            "login.error.requiredField": "The field is required",
            "login.error.emailOrPasswordInvalid": "Email or password are invalid",

            "login.signUp": "Sign up",
            "login.error.invalidEmail": "Email is not valid",
            "login.error.invalidPassword": "Password should be minimum 6 characters long",
            "login.error.notEqualPasswords": "Passwords are not the same",
            "login.error.accessDenied": "Password is invalid",
            "login.error.userAlreadyExists": "User {{existingEmail}} already exists",
        }, {
            "content.folders.newFolderName": "New folder",
            "content.numberSign": " #"
        }));

