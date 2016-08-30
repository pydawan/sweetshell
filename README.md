### ssh / linux setup
* Install VirtualBox https://www.virtualbox.org/wiki/Downloads
* Install Vagrant https://www.vagrantup.com/downloads.html
```
vagrant init bento/ubuntu-16.04
vagrant up --provider virtualbox
vagrant ssh
```

### Java 8
```
sudo apt-get install python-software-properties
sudo apt-get install software-properties-common
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
```

### deps
```
sudo apt-get install unzip
sudo apt-get install curl
```