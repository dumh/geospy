<?php
$subject = $_POST["subj"];
$provider = $_POST["prv"];
$longitude = $_POST["lon"];
$latitude = $_POST["lat"];
$accuracy = $_POST["acc"];
$speed = $_POST["spd"];

$data = implode(";", array(time(), $provider, $latitude, $longitude, $accuracy, $speed));

$f = fopen("data/".$subject."/current", 'w+') or die("can't open file");
fwrite($f, $data);
fclose($f);

$f = fopen("data/".$subject."/history", 'a') or die("can't open file");
fwrite($f, $data."\n");
fclose($f);

?>