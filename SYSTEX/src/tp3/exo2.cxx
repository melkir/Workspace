/* en-têtes standard */
#include <sys/types.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>

#include <sys/wait.h>   /* wait */
#include <ctype.h>      /* isspace */
#include <errno.h>      /* errno */
#include <string.h>

//typedef enum {
//  false,
//  true
//} bool;

char ligne[4096];       /* ligne d'entrée */

void affiche_invite()
{
  printf("> ");
  fflush(stdout);
}

void lit_ligne()
{
  if (!fgets(ligne,sizeof(ligne)-1,stdin)) {
    /* ^D ou fin de fichier => on quittte */
    printf("\n");
    exit(0);
  }
}

/* attent la fin du processus pid */
void attent(pid_t pid)
{
  /* il faut boucler car waitpid peut retourner avec une erreur non fatale */
  while (1) {
    int status;
    int r = waitpid(pid,&status,0); /* attente bloquante */
    if (r < 0) { 
      if (errno==EINTR) continue; /* interrompu => on recommence à attendre */
      printf("erreur de waitpid (%s)\n",strerror(errno));
      break;
    }
    if (WIFEXITED(status))
      printf("terminaison normale, status %i\n",WEXITSTATUS(status));
    if (WIFSIGNALED(status))
      printf("terminaison par signal %i\n",WTERMSIG(status));
    break;
  }
}

/* execute la ligne */
void execute()
{
  pid_t pid;

  /* supprime le \n final */
  if (strchr(ligne,'\n')) *strchr(ligne,'\n') = '\0';

  /* saute les lignes vides */
  if (!strcmp(ligne,"")) return;

  pid = fork();
  if (pid < 0) {
    printf("fork a échoué (%s)\n",strerror(errno));
    return;
  }

  if (pid==0) { /* fils */
    //execvp();
    execlp(
      ligne, /* programme à exécuter */
      ligne, /* argv[0], par convention le nom de programme exécuté */
      NULL   /* pas d'autre argument */
    );

    /* on n'arrive ici que si le exec a échoué */
    printf("impossible d'éxecuter \"%s\" (%s)\n",ligne,strerror(errno));
    exit(1);
  }
  else { /* père */
    attent(pid);
  }
}

int main() {
    while(1) {
        affiche_invite();
        lit_ligne();
        execute();
    }
    return 0;
}
