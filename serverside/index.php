<?php

//see https://stackoverflow.com/questions/15870159/list-files-on-directory-and-print-result-as-json

const FILELISTNAME="files_list.json";
const OLDFILELISTNAME="filelist.json";
//main: return the json file if found, else create it
$json_db_file=fopen(FILELISTNAME,"r");
    if(!$json_db_file)
        {
            create_json_file();
        }
     else
        {
            header('Content-Type: application/json');
	        echo fread($json_db_file,filesize(FILELISTNAME));
            fclose($json_db_file);
        }
//end main



function create_json_file()
    {
	    $json_db_file=fopen(FILELISTNAME,"w");
        //create json array
        $dir = "./"; //path
        $list = array(); //main array

        if(is_dir($dir))
            {
                if($dh = opendir($dir))
                    {
                        while($file = readdir($dh))
                            {

                                if($file == "." || $file == ".." || $file == OLDFILELISTNAME || $file == FILELISTNAME || $file == "index.php")
                                {
                                //dont parse theses files
                                }
                                 else
                                    { //create object with two fields
                                    $list3 = array(
                                         'name' => $file ,
                                        'size' => filesize($file),
		                        		'sum' => md5_file($file)
				                                   );
                                      array_push($list, $list3);
                                     }
                              }
                         }

                fwrite($json_db_file, json_encode($list));
	            header('Content-Type: application/json');
	            echo json_encode($list);
            }
fclose($json_db_file);
}
?>