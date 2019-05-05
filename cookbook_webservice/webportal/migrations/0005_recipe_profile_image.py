# Generated by Django 2.2.1 on 2019-05-05 09:27

from django.db import migrations, models
import webportal.models


class Migration(migrations.Migration):

    dependencies = [
        ('webportal', '0004_recipe_tags'),
    ]

    operations = [
        migrations.AddField(
            model_name='recipe',
            name='profile_image',
            field=models.ImageField(blank=True, null=True, upload_to=webportal.models.get_image_path),
        ),
    ]
