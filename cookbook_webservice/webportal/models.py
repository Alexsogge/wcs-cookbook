from django.db import models
from django.conf import settings
import os

def get_image_path(instance, filename):
    return os.path.join('images', str(instance.id), filename)


class Tag(models.Model):
    name = models.CharField(max_length=100)

    def __str__(self):
        return "{}: {}".format(self.id, self.name)

class Unit(models.Model):
    name = models.CharField(max_length=50)
    short = models.CharField(max_length=10, default="", blank=True)

    def __str__(self):
        return "{}: {}".format(self.id, self.name)

class Ingredient(models.Model):
    name = models.CharField(max_length=200)

    amount = models.FloatField()
    unit = models.ForeignKey(Unit, on_delete=models.SET_NULL, blank=True, null=True)

    def __str__(self):
        return "{}: {} x {} {}".format(self.id, self.name, self.amount, self.unit.short)

class WorkStep(models.Model):
    description = models.CharField(max_length=1000)


class Recipe(models.Model):
    name = models.CharField(max_length=200)
    description = models.TextField()
    ingredients = models.ManyToManyField(Ingredient)
    work_steps = models.ManyToManyField(WorkStep, through='RecipeWorkStep')

    tags = models.ManyToManyField(Tag)
    image = models.ImageField(blank=True, null=True)

    def get_work_steps(self):
        return self.work_steps.order_by('recipeworkstep')


class RecipeWorkStep(models.Model):
    recipe = models.ForeignKey(Recipe, on_delete=models.CASCADE)
    workstep = models.ForeignKey(WorkStep, on_delete=models.CASCADE)
    step_number = models.IntegerField(default=0)

    class Meta:
        ordering = ('step_number',)


class CookingSession(models.Model):
    owner = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.CASCADE)
    recipe = models.ForeignKey(Recipe, on_delete=models.CASCADE)
    current_step = models.IntegerField(default=0)

    start_time = models.DateTimeField(auto_now=True)


