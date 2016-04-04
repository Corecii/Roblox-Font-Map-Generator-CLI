os.execute("rm -rf ./build")
os.execute("javac -d ./build ./java/*.java")
os.execute("jar cv -f ./final/cmd.jar -e CmdController ./build/*")
