package controllers.backend;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.avaje.ebean.Ebean;
import form.OperationOfArea;
import form.OperationOfBuilding;
import form.OperationOfHouse;
import form.UpdateHouse;
import json.BuildingData;
import json.HouseData;
import json.OperationResult;
import models.*;
import play.Logger;
import play.Play;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.backend.data_manager;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import play.mvc.Http.MultipartFormData.FilePart;
import views.html.error.not_found;

import javax.imageio.ImageIO;

/**
 * Created by Summer on 16/3/17.
 */

@Restrict(@Group("ADMIN"))
public class DataManager extends Controller {
    @SuppressWarnings("unchecked")
    public Result index () {
        // get necessary data from db
        List<Area> areas = Area.find.all();
        List<BuildingKind> buildingKinds = BuildingKind.find.all();
        List<HouseState> houseStates = HouseState.find.all();
        List<Building> buildings = Building.find.all();
        List<House> houses = House.find.all();

        Optional<Admin> user = (Optional) ctx().args.get("user");
        return ok(data_manager.render("数据管理", user.get(), areas, buildingKinds, houseStates, buildings, houses));
    }

    public Result backup () {
        boolean isSucceed = false; // record cmd exec result

        String root = Play.application().path().toString();
        String dataFilePath = Paths.get(root, "data").toString();
        String scriptPath = Paths.get(root, "data", "backup.script.js").toString();

        try {
            Process process = Runtime.getRuntime().exec(new String[] {"node", scriptPath, dataFilePath});
            process.waitFor();

            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));

            String line = null;

            while ((line = input.readLine()) != null) {
                Logger.info(line);
            }

            if (process.exitValue() == 0) isSucceed = true;

            process.destroy();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (isSucceed) return ok(Json.toJson(new OperationResult(200, 0, "数据备份成功")));
        else return internalServerError(Json.toJson(new OperationResult(500, 1, "数据备份失败")));
    }

    public Result restore () {
        boolean isSucceed = false; // record cmd exec result

        String root = Play.application().path().toString();
        String dataFilePath = Paths.get(root, "data").toString();
        String scriptPath = Paths.get(root, "data", "restore.script.js").toString();

        try {
            Process process = Runtime.getRuntime().exec(new String[] {"node", scriptPath, dataFilePath});
            process.waitFor();

            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));

            String line = null;

            while ((line = input.readLine()) != null) {
                Logger.info(line);
            }

            if (process.exitValue() == 0) isSucceed = true;

            process.destroy();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (isSucceed) return ok(Json.toJson(new OperationResult(200, 0, "数据恢复成功")));
        else return internalServerError(Json.toJson(new OperationResult(500, 1, "数据恢复失败")));
    }

    @BodyParser.Of(BodyParser.MultipartFormData.class)
    public Result addArea () {
        Form<OperationOfArea> areaForm = Form.form(OperationOfArea.class).bindFromRequest();
        if (areaForm.hasErrors()) return badRequest("表单数据错误");

        // get data from form
        OperationOfArea data = areaForm.get();
        String areaName = data.getArea_name();
        Integer buildingNum = data.getBuilding_num();
        String description = data.getDescription();
        String management = data.getManagement();
        String territory = data.getTerritory();

        // get upload path
        String uploadPath = Play.application().configuration().getString("uploadPath");

        // get img
        FilePart filepart = request().body().asMultipartFormData().getFile("img");
        if (filepart == null) return badRequest("需要图片");
        File image = filepart.getFile();
        String extensionName = filepart.getFilename().split("\\.")[1];
        String uuid = UUID.randomUUID().toString();
        String imageName = uuid + "." + extensionName;
        String imageStorePath = uploadPath + imageName;

        Logger.info(imageStorePath);

        // store to db
        Area area = new Area();
        Ebean.beginTransaction();
        try {
            area.setBuildingNum(buildingNum);
            area.setDescription(description);
            area.setName(areaName);
            area.setImg(imageName);
            area.setManagement(management);
            area.setTerritory(territory);

            // 先存img在写入db
            ImageIO.write(ImageIO.read(image), extensionName, new File(imageStorePath));
            area.save();

            Ebean.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
            Ebean.rollbackTransaction();
            return internalServerError("服务器端错误");
        }
        finally {
            Ebean.endTransaction();
        }

        return redirect(controllers.backend.routes.DataManager.index());
    }

    @BodyParser.Of(BodyParser.MultipartFormData.class)
    public Result addBuilding () {
        Form<OperationOfBuilding> buildingForm = Form.form(OperationOfBuilding.class).bindFromRequest();
        if (buildingForm.hasErrors()) return badRequest("表单数据错误");

        // get info from form data
        OperationOfBuilding data = buildingForm.get();
        Long areaId = data.getArea();
        Long buildingKindId = data.getKind();
        Integer houseNum = data.getHouse_num();
        String description = data.getDescription();
        Integer acreage = data.getAcreage();

        // parse String to Calendar
        String timeString = buildingForm.get().getCompletion_date();
        Calendar completionDate = Calendar.getInstance();
        if (!timeString.equals("无")) {
            try {
                completionDate.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(timeString));
            } catch (Exception e) {
                return badRequest("时间格式不对");
            }
        }

        // get upload path
        String uploadPath = Play.application().configuration().getString("uploadPath");

        // get image
        FilePart filepart = request().body().asMultipartFormData().getFile("img");
        if (filepart == null) return badRequest("需要图片");
        File image = filepart.getFile();
        String extensionName = filepart.getFilename().split("\\.")[1];
        String uuid = UUID.randomUUID().toString();
        String imageName = uuid + "." + extensionName;
        String imageStorePath = uploadPath + imageName;

        Logger.info(imageStorePath);

        // get area ref
        Area area = Area.find.byId(areaId);
        if (area == null) return badRequest("小区不存在");

        // get building kind ref
        BuildingKind buildingKind = BuildingKind.find.byId(buildingKindId);
        if (buildingKind == null) return badRequest("楼栋类型不存在");

        // store to db
        Building building = new Building();
        Ebean.beginTransaction();
        try {
            building.setArea(area);
            building.setBuildingKind(buildingKind);
            building.setAcreage(acreage);
            building.setDescription(description);
            if (timeString.equals("无")) building.setCompletionDate(null);
            else building.setCompletionDate(completionDate);
            building.setHouseNum(houseNum);
            building.setImg(imageName);

            // 先存img在写入db
            ImageIO.write(ImageIO.read(image), extensionName, new File(imageStorePath));
            building.save();

            Ebean.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
            Ebean.rollbackTransaction();
            return internalServerError("服务器端错误");
        }
        finally {
            Ebean.endTransaction();
        }

        return redirect(controllers.backend.routes.DataManager.index());
    }

    @BodyParser.Of(BodyParser.MultipartFormData.class)
    public Result addHouse () {
        Form<OperationOfHouse> houseForm = Form.form(OperationOfHouse.class).bindFromRequest();
        if (houseForm.hasErrors()) return badRequest("表单数据错误");

        // get data from form
        OperationOfHouse data = houseForm.get();
        Long buildingId = data.getBuilding();
        Integer floor = data.getFloor();
        Integer no = data.getNo();
        Long stateId = data.getState();
        Integer space = data.getSpace();
        Integer price = data.getPrice();

        // get of building ref
        Building building = Building.find.byId(buildingId);
        if (building == null) return badRequest("楼栋不存在");

        // get house state ref
        HouseState state = HouseState.find.byId(stateId);
        if (state == null) return badRequest("房屋状态不存在");

        // get upload path
        String uploadPath = Play.application().configuration().getString("uploadPath");

        // get image
        FilePart filepart = request().body().asMultipartFormData().getFile("img");
        if (filepart == null) return badRequest("需要图片");
        File image = filepart.getFile();
        String extensionName = filepart.getFilename().split("\\.")[1];
        String uuid = UUID.randomUUID().toString();
        String imageName = uuid + "." + extensionName;
        String imageStorePath = uploadPath + imageName;

        Logger.info(imageStorePath);

        // save to db
        House house = new House();
        Ebean.beginTransaction();
        try {
            house.setBuildingId(building);
            house.setState(state);
            house.setPricePerSM(price);
            house.setImg(imageName);
            house.setFloor(floor);
            house.setHouseNo(no);
            house.setSpace(space);

            // 先存img在写入db
            ImageIO.write(ImageIO.read(image), extensionName, new File(imageStorePath));
            house.save();

            Ebean.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
            Ebean.rollbackTransaction();
            return internalServerError("服务器端错误");
        }
        finally {
            Ebean.endTransaction();
        }

        return redirect(controllers.backend.routes.DataManager.index());
    }

    @BodyParser.Of(BodyParser.MultipartFormData.class)
    public Result updateArea (Long id) {
        Form<OperationOfArea> areaForm = Form.form(OperationOfArea.class).bindFromRequest();
        if (areaForm.hasErrors()) return badRequest("表单数据错误");

        // get data from form
        OperationOfArea data = areaForm.get();
        String areaName = data.getArea_name();
        Integer buildingNum = data.getBuilding_num();
        String description = data.getDescription();
        String management = data.getManagement();
        String territory = data.getTerritory();

        // get upload path
        String uploadPath = Play.application().configuration().getString("uploadPath");

        // get img
        boolean isImgNeedToUpdate = true;

        FilePart filepart = request().body().asMultipartFormData().getFile("img");
        if (filepart == null) isImgNeedToUpdate = false;

        File image = null;
        String imageName = "";
        String imageStorePath = "";
        String extensionName = "";

        if (isImgNeedToUpdate) {
            image = filepart.getFile();
            extensionName = filepart.getFilename().split("\\.")[1];
            String uuid = UUID.randomUUID().toString();
            imageName = uuid + "." + extensionName;
            imageStorePath = uploadPath + imageName;
        }

        Logger.info(imageStorePath);

        // get area ref
        Area area = Area.find.byId(id);
        if (area == null) return badRequest("小区不存在");

        // update value and store to db
        Ebean.beginTransaction();
        try {
            area.setBuildingNum(buildingNum);
            area.setDescription(description);
            area.setName(areaName);
            area.setTerritory(territory);
            if (isImgNeedToUpdate) area.setImg(imageName);
            area.setManagement(management);

            // 先存img再写入db
            if (isImgNeedToUpdate) ImageIO.write(ImageIO.read(image), extensionName, new File(imageStorePath));

            area.save();

            Ebean.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
            Ebean.rollbackTransaction();
            return internalServerError("服务器端错误");
        }
        finally {
            Ebean.endTransaction();
        }

        return redirect(controllers.backend.routes.DataManager.index());
    }

    @BodyParser.Of(BodyParser.MultipartFormData.class)
    public Result updateBuilding (Long id) {
        Form<OperationOfBuilding> buildingForm = Form.form(OperationOfBuilding.class).bindFromRequest();
        if (buildingForm.hasErrors()) return badRequest("表单数据错误");

        // get info from form data
        OperationOfBuilding data = buildingForm.get();
        Long areaId = data.getArea();
        Long buildingKindId = data.getKind();
        Integer houseNum = data.getHouse_num();
        String description = data.getDescription();
        Integer acreage = data.getAcreage();

        // parse String to Calendar
        String timeString = buildingForm.get().getCompletion_date();
        Logger.info(timeString.equals("无") + "");
        Calendar completionDate = Calendar.getInstance();
        if (!timeString.equals("无")) {
            try {
                completionDate.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(timeString));
            } catch (Exception e) {
                return badRequest("时间格式不对");
            }
        }

        // get upload path
        String uploadPath = Play.application().configuration().getString("uploadPath");

        // get img
        boolean isImgNeedToUpdate = true;

        FilePart filepart = request().body().asMultipartFormData().getFile("img");
        if (filepart == null) isImgNeedToUpdate = false;

        File image = null;
        String imageName = "";
        String imageStorePath = "";
        String extensionName = "";

        if (isImgNeedToUpdate) {
            image = filepart.getFile();
            extensionName = filepart.getFilename().split("\\.")[1];
            String uuid = UUID.randomUUID().toString();
            imageName = uuid + "." + extensionName;
            imageStorePath = uploadPath + imageName;
        }

        Logger.info(imageStorePath);

        // get building ref
        Building building = Building.find.byId(id);
        if (building == null) return badRequest("楼栋不存在");

        // get area ref
        Area area = null;
        if (areaId != -1) {
            area = Area.find.byId(areaId);
            if (area == null) return badRequest("小区不存在");
        }

        // get building kind ref
        BuildingKind buildingKind = null;
        if (buildingKindId != -1) {
            buildingKind = BuildingKind.find.byId(buildingKindId);
            if (buildingKind == null) return badRequest("楼栋类型不存在");
        }

        // store to db
        Ebean.beginTransaction();
        try {
            if (areaId != -1) building.setArea(area);

            if (buildingKindId != -1) building.setBuildingKind(buildingKind);

            building.setAcreage(acreage);

            building.setDescription(description);

            if (timeString.equals("无")) building.setCompletionDate(null);
            else building.setCompletionDate(completionDate);

            building.setHouseNum(houseNum);

            if (isImgNeedToUpdate) building.setImg(imageName);

            // 先存img再写入db
            if (isImgNeedToUpdate) ImageIO.write(ImageIO.read(image), extensionName, new File(imageStorePath));

            building.save();

            Ebean.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
            Ebean.rollbackTransaction();
            return internalServerError("服务器端错误");
        }
        finally {
            Ebean.endTransaction();
        }

        return redirect(controllers.backend.routes.DataManager.index());
    }

    @BodyParser.Of(BodyParser.MultipartFormData.class)
    public Result updateHouse (Long id) {
        Form<UpdateHouse> houseForm = Form.form(UpdateHouse.class).bindFromRequest();
        if (houseForm.hasErrors()) return badRequest("表单数据错误");

        // get data from form
        UpdateHouse data = houseForm.get();

        Integer floor = data.getFloor();
        Integer no = data.getNo();
        Long stateId = data.getState();
        Integer space = data.getSpace();
        Integer price = data.getPrice();

        String buyDate = data.getBuy_date();
        Calendar buyDateCal = Calendar.getInstance();
        if (!buyDate.equals("无")) {
            try {
                buyDateCal.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(buyDate));
            } catch (Exception e) {
                return badRequest("时间格式不对");
            }
        }

        String inDate = data.getIn_date();
        Calendar inDateCal = Calendar.getInstance();
        if (!inDate.equals("无")) {
            try {
                inDateCal.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(inDate));
            } catch (Exception e) {
                return badRequest("时间格式不对");
            }
        }

        // get house state ref
        HouseState state = null;
        if (stateId != -1) {
            state = HouseState.find.byId(stateId);
            if (state == null) return badRequest("小区不存在");
        }

        // get upload path
        String uploadPath = Play.application().configuration().getString("uploadPath");

        // get image
        boolean isImgNeedToUpdate = true;

        FilePart filepart = request().body().asMultipartFormData().getFile("img");
        if (filepart == null) isImgNeedToUpdate = false;

        File image = null;
        String imageName = "";
        String imageStorePath = "";
        String extensionName = "";

        if (isImgNeedToUpdate) {
            image = filepart.getFile();
            extensionName = filepart.getFilename().split("\\.")[1];
            String uuid = UUID.randomUUID().toString();
            imageName = uuid + "." + extensionName;
            imageStorePath = uploadPath + imageName;
        }

        Logger.info(imageStorePath);

        // get house ref
        House house = House.find.byId(id);
        if (house == null) return badRequest("房屋不存在");

        // save to db
        Ebean.beginTransaction();
        try {
            if (stateId != -1) house.setState(state);
            house.setPricePerSM(price);
            house.setFloor(floor);
            house.setHouseNo(no);
            house.setSpace(space);

            if (inDate.equals("无")) house.setInDate(null);
            else house.setInDate(inDateCal);

            if (buyDate.equals("无")) house.setBuyDate(null);
            else house.setBuyDate(buyDateCal);

            if (isImgNeedToUpdate) house.setImg(imageName);

            // 先存img再写入db
            if (isImgNeedToUpdate) ImageIO.write(ImageIO.read(image), extensionName, new File(imageStorePath));

            house.save();

            Ebean.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
            Ebean.rollbackTransaction();
            return internalServerError("服务器端错误");
        }
        finally {
            Ebean.endTransaction();
        }

        return redirect(controllers.backend.routes.DataManager.index());
    }

    public Result getArea (Long id) {
        Area area = Area.find.byId(id);
        if (area == null) return notFound("未找到此小区");

        return ok(Json.toJson(area));
    }

    public Result getBuilding (Long id) {
        Building building = Building.find.byId(id);
        if (building == null) return notFound("未找到此楼栋");

        BuildingData data = new BuildingData();
        data.setAcreage(building.getAcreage());
        data.setArea(building.getArea().getName());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar completionDateCal = building.getCompletionDate();
        String completionDate = "无";
        if (completionDateCal != null) completionDate = sdf.format(completionDateCal.getTime());

        data.setCompletion_date(completionDate);

        data.setDescription(building.getDescription());
        data.setHouse_num(building.getHouseNum());
        data.setImg(building.getImg());
        data.setKind(building.getBuildingKind().getName());

        return ok(Json.toJson(data));
    }

    public Result getHouse (Long id) {
        House house = House.find.byId(id);
        if (house == null) return notFound("未找到此房屋");

        HouseData data = new HouseData();
        data.setFloor(house.getFloor());
        data.setNo(house.getHouseNo());
        data.setState(house.getState().getName());
        data.setSpace(house.getSpace());
        data.setPrice(house.getPricePerSM());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar buyDateCal = house.getBuyDate();
        Calendar inDateCal = house.getInDate();
        String buyDate = "无";
        String inDate = "无";
        if (buyDateCal != null) buyDate = sdf.format(buyDateCal.getTime());
        if (inDateCal != null) inDate = sdf.format(inDateCal.getTime());

        data.setBuy_date(buyDate);
        data.setIn_date(inDate);
        data.setImg(house.getImg());

        return ok(Json.toJson(data));
    }

    public Result deleteArea(Long id) {
        return redirect(controllers.backend.routes.DataManager.index());
    }

    public Result deleteBuilding(Long id) {
        return redirect(controllers.backend.routes.DataManager.index());
    }

    public Result deleteHouse(Long id) {
        House house = House.find.byId(id);
        if (house == null) return notFound(not_found.render("404", "你访问的房屋不存在"));

        Ebean.beginTransaction();
        try {
            Integer origin = house.getBuildingId().getHouseNum();
            Integer now = origin - 1;
            house.getBuildingId().setHouseNum(now);
            house.delete();
        }
        catch (Exception e) {
            e.printStackTrace();
            Ebean.rollbackTransaction();
            return internalServerError("服务端错误");
        }
        finally {
            Ebean.commitTransaction();
        }

        return redirect(controllers.backend.routes.DataManager.index());
    }
}
