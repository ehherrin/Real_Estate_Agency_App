# How to build and run it
1. Load up the real-estate database in intellij
2. Make sure of the following:
    - "source root directory" is marked(should only have one marked)
    - "out directory" exists
    - "csv files" exist
    - sdk is set to java 11
3. Add the H2 database as a jar file dependency to the real-estate database
    - Go to **File** > **Project Structure**
    - Underneath Project Settings is exist a tab call **Module**, click it
    - You will see 3 tabs underneath the project name, click **Dependencies**
    - To the far right you will see a "+" button, click it and add the path
     that has the H2 database jar file with the jar file included in the path
4. Recompile and run the Real-Estate database.
5. If you fail to get everything working, git nuke it and restart the steps on how to build and run it
    when you have tried everything else    
