from django.db import models

# Create your models here.


class Tag(models.Model):
    name = models.CharField(max_length=100)

class Unit(models.Model):
    name = models.CharField(max_length=50)

class Ingredient(models.Model):
    name = models.CharField(max_length=200)

    amount = models.FloatField()
    unit = models.ForeignKey(Unit, on_delete=models.SET_NULL, blank=True, null=True)

class WorkStep(models.Model):
    description = models.CharField(max_length=1000)


class Recipe(models.Model):
    name = models.CharField(max_length=200)
    description = models.TextField()
    work_steps = models.ManyToManyField(WorkStep, through='RecipeWorkStep')

    def get_work_steps(self):
        return self.work_steps.order_by('link_to_recipe')


class RecipeWorkStep(models.Model):
    recipe = models.ForeignKey(Recipe, on_delete=models.CASCADE)
    workstep = models.ForeignKey(WorkStep, on_delete=models.CASCADE)
    step_number = models.IntegerField(default=0)

    class Meta:
        ordering = ('step_number',)