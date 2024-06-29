<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class FooController extends AbstractController
{
    #[Route('/bar', name: 'bar')]
    public function barAction(): Response
    {
        $numA = 1;
        $numB = $numA;
        return new Response("echo $$numB");
    }

    #[Route('/params/{name}', name: 'params')]
    public function params($name): Response
    {
        return new Response("echo $name");
    }

    #[Route('/render', name: 'render')]
    public function renderAction()
    {
        return $this->render('base.html.twig');
    }
}