# AMS
College Attendance Management System

AMS is an android application that provides a system to manage the attendance through QR Code Scanning. It uses device info and
location services to verify the attendance and hence prevent proxy in any form.

This app uses PHP server as well as Fireabase services to store and manage attendance and user records.

## Fetures:

* One can register as student or as teacher. Teacher has to choose the subjects in different department and semesters.
  If anyone is registering as teacher, a notification is sent to the admin regarding the verification request and admin
  choose to verify the teacher profile by reviewing the subjects the teacher has selected. When the admin verifies the
  teacher, only then a teacher will be able to view the dashboard.
  
 * Once admin verifies the teacher, a push notification is sent to that teacher regarding the same.
 
 * The teacher has option to choose the subject of which the attendance is to be taken. A QR Code will be generated with
  credentials that verify the subject.
  
  * The students will then scan the QR Code and the corresponding attendance will be marked. The credibility of the students'
  info is verified by device info of the students and its location.
  
  * Students can view their attendance in any subject by selecting that subject from the dashboard.
  
  * Teacher can generate a list of those students whose atteadance is less than 75 %. He/She can even send an email to
  such students.
  
  * Teacher can view the profile and students info by scnannig the unique QR Code of student.
  
