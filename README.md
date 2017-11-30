# TestTwitterApp
This app lets the user log in to any Twitter account, uses android speech recognition by Google to recognize what the user tells and tweets it.

This app utilizes two APIs -- Twitter REST API-- and --Google Speech Recognition API--. 

HOW IT WORKS:
1.-The app either recognizes a previously authenticated Twitter and goes straight to the main activity 
![Alt text](/relative/path/to/img.jpg?raw=true "Optional Title")
OR
authenticates the user according to a previously created Twitter account.
2.-Displays the a "Hello"+username message along with a "LOG OUT" button and a "Record" button.
3.-When the user selects the record button, the app utilizes the Google Speech Recognition Intent to recognize what the user talked about.
3.- The app triggerrs an Activity that lets the user modify their message before tweeting, in case the Speech Recognition did not caught something right.
4.-The app utilizes the Twitter REST API to tweet it into the desired account once the user clicks on the tweet button.
