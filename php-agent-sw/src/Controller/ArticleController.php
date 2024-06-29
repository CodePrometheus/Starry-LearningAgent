<?php

namespace App\Controller;

use App\Entity\Article;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\Routing\Annotation\Route;

class ArticleController extends AbstractController
{
    #[Route('/insertArticle', name: 'insertArticle')]
    public function insertArticle(ManagerRegistry $doctrine)
    {
        $manager = $doctrine->getManager();
        $article = new Article();
        $article->setTitle("un titre");
        $article->setDescribe("une description");
        $manager->persist($article);
        $manager->flush();
        return $this->render("article.html.twig", [
            "article" => $article,
        ]);
    }
}