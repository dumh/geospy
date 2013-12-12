<?php
$subject = $_GET["subj"];
$from_ts = $_GET["from"];
$to_ts = $_GET["to"];

if ($from_ts && !$to_ts) {
    $to_ts = time();
}

$history = ($from_ts != null);

$f = fopen("data/".$subject."/".($history?"history":"current"), 'r') or die("can't open file");
$track = array();
while (($line = fgets($f)) !== false) {
    $parts = explode(";", $line, 7);
    for ($i = 0; $i < count($parts); $i++) {
        $parts[$i] = str_replace("\n", "", $parts[$i]);
    }
    if ($history) {
        $time = $parts[0];
        if ($time < $from_ts || $time > $to_ts) {
            continue;
        }
    }
    array_push($track, array(
        "time" => $parts[0],
        "provider" => $parts[1],
        "latitude" => $parts[2],
        "longitude" => $parts[3],
        "accuracy" => $parts[4],
        "speed" => $parts[5]
    ));
    if (!$history) {
        $track = $track[0];
        break;
    }
}
fclose($f);

echo json_encode($track);

?>
