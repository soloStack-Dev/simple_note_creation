# Requirement about project

## Build the application with security Authentications

- The app have a three nav links

# Nav Links and page structure
- Home: this is main page the page in front of the page 
- In the center add this content 'Describe your life on memories' and in sub line next to be
- add about simple two line about this note website like the website use for write thier user life stories and memories and thught and ideas 
- whatever it is his daily life to describe it
- About: this is the page tell the purpose why we create this website and it purpose 
- Note: in this page left corner have a button[add new note] when user click the button popup panel to shown 
- panel in the panel shown [Catagories(daily routine,memories,something else) - select the catagory, Message panel - textarea input give large text input,button(create) - after hit button to notes was created in the note page grid formate aligned ]
- block of notes look like a [catagories(what user was select) and message look and in this block have [edit and delete button - each button have an purpose edit have to update the notes and delete is an remove the notes in note page]]
- no notes was highlight in the note page shown the message[no notes are available]

# Security in the webpage 

- when user in the home page in the page top corner add button for authentication [SignUP/LogIn]
- when the user was click [SignUp -  button to redirect path to /signup shown the panel[ Username, Email, Password] after athenticate to go in this page / - home page and the corner shown profile icon(when user was tap the icon shown small drop down panel shown profile [username, email, bio with input feild and save botton])] and [LogIn - button to redirect path to /LogIn shwon the panel[ Username, Password] after autenticate to go in this page / - home page and the corner shown profile icon(when user was tap the icon shown small drop down panel shown profile [username, email, bio with input feild and save botton])] 
- and the profile behind shown the button[SignOut - when user click the button to rediect in home page / - the page again shown [SignUp/LoginIn] top of the corner in the page]
- Inthis athentication is Token based authentication
- In the security on application.properties add the spring seccruity [username,email,password = {user authentication based inputs}]

# Database

- I added the H2 inmemory database the database have a data i describe the structure to store what kind of datas

- In the databse store SignUp authentication datas like [Id, Username, Email, password in ecoding formate and Generated tokens,created timezone and date,token expiration timezone]
- Table name AuthSource and DB name NoteDB
- In the same database create another table name notes the table was store data like a [Id, catagory,message,createdAt time]
- Error log table the table name ErrorEnquiry structure like a [ Id, errorTitle,errorLog_Message,Describetion about errors,Issue fixed info,createdAt timezone]

# Database security

- when the website owner review the data in H2 database in the path /h2-console before redirect to shown the authentication page /login after authenticate to go the /h2-console

# Docker file creation

- Complete the development create docker file and compose the file push the container in docker desktop 

# logs and validations

- if you face some error the error logs and how you fix the error the actions describe to store the database i describe the structure every runtime error log you find instancely store in the h2 database

# Reference 

- Take a this three file frontend reference [bootstrap-5.3-coding-agent-reference.md, htmx-project-reference.md, css-project-reference.md] 
- reference with AGENTS.md