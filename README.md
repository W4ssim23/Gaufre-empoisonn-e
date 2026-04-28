# Gaufre Empoisonnée

Membres du groupe :
- MADJOUR Amir
- ZOUITEN Ouassim
- BOUGHERRA Lakhdar
- DJERBOA Mohamed ilyes
- FRITAH Wassim
- SAHRAOUI Ilyes 

Ce document explique comment compiler, exécuter et générer un fichier JAR pour cette application.

## 1. Compiler l'application

Pour compiler le code source (qui se trouve dans le dossier `src`) et placer les fichiers compilés (`.class`) dans le dossier `bin`, utilisez la commande suivante :

```bash
# Créer le dossier bin s'il n'existe pas déjà
mkdir bin

# Compiler les fichiers Java
javac -d bin -sourcepath src src/Main.java
```

## 2. Exécuter l'application

vous pouvez lancer le jeu avec :

```bash
java -cp bin Main
```

## 3. Créer un fichier JAR exécutable

Le projet inclut un fichier `MANIFEST.MF` qui spécifie déjà la classe principale (`Main`).

Pour générer le fichier JAR, exécutez la commande suivante à la racine du projet :

```bash
jar cmf MANIFEST.MF GaufreEmpoisonnee.jar -C bin .
```

### Exécuter le fichier JAR

Vous pouvez jouer avec :

```bash
java -jar GaufreEmpoisonnee.jar
```
Vous pouvez également lancer le jeu en double-cliquant simplement sur le fichier `.jar`.
