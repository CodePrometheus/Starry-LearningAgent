<?php

// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use RdKafka\Conf;
use RdKafka\KafkaConsumer;
use RdKafka\Producer;

class KafkaController extends AbstractController
{
    #[Route('/kafka', name: 'kafka')]
    public function kafka(): Response
    {
        $conf = new \RdKafka\Conf();
        $conf->set('metadata.broker.list', '127.0.0.1:9092');
        $producer = new \RdKafka\Producer($conf);
        $topic = $producer->newTopic('sw-topic');
        $topic->produce(RD_KAFKA_PARTITION_UA, 0, 'I love skywalking 3 thousand');
        $producer->poll(0);
        $result = $producer->flush(10000);
        if (RD_KAFKA_RESP_ERR_NO_ERROR !== $result) {
            throw new \RuntimeException('Was unable to flush, messages might be lost!');
        }

        $conf = new \RdKafka\Conf();
        $conf->set('group.id', 'sw-group');
        $conf->set('metadata.broker.list', '127.0.0.1');
        $conf->set('auto.offset.reset', 'earliest');
        $consumer = new \RdKafka\KafkaConsumer($conf);
        $consumer->subscribe(['sw-topic']);

        $message = $consumer->consume(30 * 1000);
        switch ($message->err) {
            case RD_KAFKA_RESP_ERR_NO_ERROR:
                echo "ok";
                break;
            case RD_KAFKA_RESP_ERR__PARTITION_EOF:
                echo "No more messages; will wait for more\n";
                break;
            case RD_KAFKA_RESP_ERR__TIMED_OUT:
                echo "Timed out\n";
                break;
            default:
                throw new \Exception($message->errstr(), $message->err);
        }
        
        return new Response("echo kafka");
    }

}