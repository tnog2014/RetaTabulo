. ./env.conf
mvn package  -Dmaven.test.skip=true
sudo mkdir -p "${destDir}"
sudo mkdir -p "${destDir}/log"
sudo cp -p "target/${jarName}" "${destDir}"
sudo cp -r "config" "${destDir}"
sudo chown -R bootapp:bootapp "${destDir}"
sudo chmod 500 "${destDir}/${jarName}"
sudo chmod 700 "${destDir}/config"
sudo chmod 700 "${destDir}/log"
sudo ln -s "${destDir}/${jarName}" "/etc/init.d/${appName}"
sudo service ${appName} restart


