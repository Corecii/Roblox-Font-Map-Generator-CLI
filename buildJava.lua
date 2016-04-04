os.execute("javac -d . ./java/*.java")
os.execute("jar cvfe ./final/cmd.jar CmdController ./*.class")
os.execute("rm *.class")
