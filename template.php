<?php
    session_start();
    if(!isset($_SESSION['logged_in'])) { 
        header('Location: /modmaker');
        die();
    }
    if(!isset($_GET['id'])) {
        header('Location: /modmaker/control-panel');
        die();
    }
    $root = $_SERVER['DOCUMENT_ROOT'];
    require($root.'/modmaker/classes/mod.php');
    $validDifficulty = false;
    $id = $_GET['id'];
    $authorized = true;
    //get mod information
    $mod = new Mod($id);
    $defaultsmod = null; //bioware mod
    if ($mod->mod_author_uid != $_SESSION['uid']) {
        //user shouldn't be able to edit this...
        $authorized = false;
    }
    $status = null;
    $definedin = null;
    $possessionarray = null;
    $defaultsarray = null;
    if ($authorized) {
        $defaultsmod = new Mod(1);
        $defaultsmod->loadWeapon();
        $defaultsmod->weapon->loadWeaponLOADNAME();
        $mod->loadWeapon();
        $mod->weapon->loadWeaponLOADNAME();
        if (isset($_SESSION['WEAPON_NAME_status'])){
            foreach ($_SESSION['WEAPON_NAME_status'] as $msg) {
                $status .= "<p class=\"error_text\"> ";
                $status .= $msg;
                $status .= "</p>\n";
            }
            unset($_SESSION['WEAPON_NAME_status']);
        }
    }
?>
<!DOCTYPE html>
<html>
<head>
    <meta content="text/html;charset=utf-8" http-equiv="Content-Type">
    <?php require ($root."/links.php"); ?>
    <link href="/styles/gallery.css" type="text/css" rel="stylesheet"/>
    <title>HUMAN_NAME | <?=$mod->mod_name;?> | Mass Effect 3 Tweaks ModMaker</title>
    <link rel="stylesheet" type="text/css" href="/styles/tooltipster.css" />
    <script src="/js/jquery.freetile.js"></script>
    <script src="/modmaker/js/attribute_tiler.js"></script>
    <script src="/modmaker/js/tooltipster.min.js"></script>
    <script src="https://ajax.aspnetcdn.com/ajax/jquery.validate/1.13.0/jquery.validate.min.js"></script>
    <script src="/modmaker/js/gen2/weapons/weapons-all.js"></script>

</head>
<body>
<?php 
    require ($root."/nav.php"); 
    require ($root."/modmaker/nav.php");
    require ($root."/ad.html");?>
    <div class="content gallery">
        <?php if (!$authorized) { ?>
                <h1 class="page_title">Not authorized</h1>
                <p class="centered">You are not authorized to edit this mod.</p>
            <?php } else { //valid mod ?>
        <div class="up_navigation">
            <a href="/modmaker/edit/<?=$id?>/weapons" class="roundedbutton">&laquo; Back to weapon list</a>
        </div>
        <h1 class="page_title"><?=$mod->mod_name;?> HUMAN_NAME</h1>
        <?php 
        $modifier_class = "unmodified";
        $modifier = "Unmodified from Genesis";
        if ($mod->weapon->mod_weapon_WEAPON_NAME_modified) {
            $modifier_class = "modified_fork";
            $modifier = "Modified since forked";
        } else if ($mod->weapon->mod_weapon_WEAPON_NAME_modified_genesis) {
            $modifier_class = "modified_genesis";
            $modifier = "Modified since Genesis";
        }
        echo "<span class=\"".$modifier_class."\">".$modifier."</span>";
        if (!is_null($status)) {
            echo $status;
        }?>
        <p>DESCRIPTION_TEXT</p>
        BALANCE_CHANGES_AREA
        <form method="post" action="/modmaker/handlers/weapons" id="form">
            <div class="attributes">
                <input type="hidden" name="action" value="WEAPON_NAME">
                <input type="hidden" name="id" value="<?=$mod->mod_id;?>">
                <!-- WEAPON MAGAZINE -->
                <div class="modmaker_attribute_wrapper">
                    <img class="guide purple_card" src="/images/modmaker/weapons/shared/thermalclip.jpg">
                    <h2 class="modmaker_attribute_title">Magazine &amp; Ammo</h2>
                    <p>Sets how the magazine and ammo is used.</p>
AMMOPERSHOT_BLOCK
ROUNDSPERBURST_BLOCK
MAGSIZE_BLOCK
SPAREAMMO_BLOCK
RELOADSPEED_BLOCK
                </div>
DAMAGE_CONTAINER
PENETRATION_CONTAINER
CHARGING_CONTAINER
WEIGHT_BLOCK
RPM_CONTAINER
CHAKRAM_CONTAINERPUNISHER_CONTAINERTYPHOON_CONTAINERSILENCER_CONTAINERVENOM_CONTAINER
RECOIL_CONTAINER
ACCURACYPENALTY_CONTAINER
ACCURACYERROR_CONTAINER
            </div>
            <input class="button green submit" type="submit" name="commit" value="Save">
        </form>
        <?php } ?>
        </div>
    </div>
<?php require($root."/footer.php"); ?>
</body>
</html>
