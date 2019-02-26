La compilation et l'execution des parties 1 et 2 fonctionne comme le code qui est fournie sur moodle. Le code des dossier client, server et shared doivent etre dans les memes dossiers respectivement.

La compilation se fait a l'aide de la commande "ant".
L'execution du serveur se fait a l'aide de la commande "./server.sh hostname"


Partie 1

L'execution du client se fait comme specifie dans l'enonce du TP.


Partie 2

Pour l'execution du client, il ne faut pas specifie le hostname en parametre. Le hostname est "hardcoded" dans le code sous la constante de la classe client DISTANT_SERVER.

De la meme facon l'option justUnread pour la methode list est une constante boolean ONLY_UNREAD_MAIL.

Les commandes du client sont specifie comme constantes juste en dessous avec un exemple d'utilisation pour chaque commande.

IMPORTANT: Les dossiers "clientDB" et "serverDB" doivent absolument se trouver dans les meme dossier que leur executable respectif client.sh et server.sh. On trouve dans serverDB le fichier "users.txt" qui indique les utilisateurs du systeme et leur mot de passe respectif. 

