	<?php
	//najbardziej wysuniete koordynaty to 54.24122,18.21451 oraz 54.63232,18.9342 czyli mamy 0,3911 i 0,7197 
	function haversineGreatCircleDistance(
	  $latitudeFrom, $longitudeFrom, $latitudeTo, $longitudeTo, $earthRadius = 6371000)
	{
	  // convert from degrees to radians
	  $latFrom = deg2rad($latitudeFrom);
	  $lonFrom = deg2rad($longitudeFrom);
	  $latTo = deg2rad($latitudeTo);
	  $lonTo = deg2rad($longitudeTo);

	  $latDelta = $latTo - $latFrom;
	  $lonDelta = $lonTo - $lonFrom;

	  $angle = 2 * asin(sqrt(pow(sin($latDelta / 2), 2) +
		cos($latFrom) * cos($latTo) * pow(sin($lonDelta / 2), 2)));
	  return $angle * $earthRadius;
	}

	$longitiude = 18.559591;
	$latitiude = 54.418455;

	$latBase = 5424;
	$lonBase = 1821;

set_time_limit(1200);
	ini_set("allow_url_fopen", 1);
	$json = file_get_contents('http://91.244.248.19/dataset/c24aa637-3619-4dc2-a171-a23eec8f2172/resource/cd4c08b5-460e-40db-b920-ab9fc93c1a92/download/stops.json');
	$obj = json_decode($json);
	$array = $obj->{'2017-06-19'}->{'stops'};
	//var_dump($array);

	$todayways = array();
	for($x =0;$x<40;$x++)
		for($y =0;$y<73;$y++)
		{	$smallestway = 1000.0;
			$smallestId = -1;
			foreach($array as $row){
				$temp_way =  haversineGreatCircleDistance(($x+$latBase)/100,($y + $lonBase)/100,$row -> {'stopLat'},$row -> {'stopLon'});
				if($temp_way < $smallestway){
					$smallestway = $temp_way;
					$smallestId = $row->{'stopId'};
					}
			}
			$todayways[$x][$y] = $smallestId;
		}
	var_dump(json_encode($todayways));
	?>