#This bash script compile and install the latest P2P version of P2P BAY.

echo "[IMPORTANT] you need openjdk to run the app."
echo "Run 'sudo apt install openjdk-11-jdk -y' to install it."
echo "-------------------------------------------------------"
echo "Compiling project..."

javac ./src/P2P_WEB/*.java -encoding utf8; mkdir ./bin/P2P_WEB ; mv ./src/P2P_WEB/*.class ./bin/P2P_WEB/

echo "Done"

echo "Generating binary..."

echo "java -cp ./bin P2P_WEB.MainWindow" > Client
echo "echo 'Please fire up a webserver in FTP-P2P/Web/ServerConsol/index.html and open your browser...'; java -cp ./bin P2P_WEB.MainMasterRepo > ./Web/ServerConsol/serverLog" > Repository
